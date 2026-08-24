package id.patunganku.event_service.service.impl;

import id.patunganku.event_service.domain.entity.Event;
import id.patunganku.event_service.domain.entity.Expense;
import id.patunganku.event_service.domain.entity.ExpenseSplit;
import id.patunganku.event_service.domain.entity.Member;
import id.patunganku.event_service.domain.entity.enumeric.EventStatus;
import id.patunganku.event_service.domain.model.dto.EventDto;
import id.patunganku.event_service.domain.model.dto.ExpenseDto;
import id.patunganku.event_service.domain.model.dto.SplitDto;
import id.patunganku.event_service.domain.model.vo.EventVo;
import id.patunganku.event_service.domain.model.vo.ExpenseVo;
import id.patunganku.event_service.config.exception.CustomException;
import id.patunganku.event_service.repository.EventRepository;
import id.patunganku.event_service.repository.ExpenseRepository;
import id.patunganku.event_service.repository.MemberRepository;
import id.patunganku.event_service.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public EventDto create(EventVo vo) {
        log.debug("[CREATE_EVENT] Start creating event with vo: {}", vo);
        Event event = Event.builder()
                .name(vo.getName())
                .createdBy(vo.getCreatedBy())
                .status(EventStatus.ACTIVE)
                .build();

        List<Member> members = vo.getMemberNames().stream()
                .map(name -> Member.builder().name(name).event(event).build())
                .collect(Collectors.toList());
        event.setMembers(members);
        eventRepository.save(event);
        log.info("[CREATE_EVENT] Event created successfully");

        return EventDto.from(event);
    }

    @Override
    public EventDto getById(Long id) {
        return EventDto.from(findById(id));
    }

    @Override
    @Transactional
    public ExpenseDto addExpense(Long id, ExpenseVo vo) {
        log.info("[ADD_EXPENSE] Start. id: {} and vo: {}", id, vo);

        Event event = findById(id);
        Member paidBy = memberRepository.findById(vo.getPaidByMemberId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Member_NOT_FOUND", "Member not found"));


        Expense expense = Expense.builder()
                .event(event)
                .paidBy(paidBy)
                .description(vo.getDescription())
                .amount(vo.getAmount())
                .build();

        List<ExpenseSplit> splits = buildSplits(vo, event, expense);
        expense.setSplits(splits);

        Expense savedExpense = expenseRepository.save(expense);

        log.info("[ADD_EXPENSE] Success. expenseId={}, id={}, paidByMemberId={}, splitCount={}, amount={}",
                savedExpense.getId(),
                id,
                paidBy.getId(),
                splits.size(),
                savedExpense.getAmount()
        );

        return ExpenseDto.from(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(Long id) {

        List<ExpenseDto> expenses = expenseRepository.findByEventId(id)
                .stream()
                .map(ExpenseDto::from)
                .toList();

        BigDecimal totalSpending = totalEventSpending(expenses);
        Map<String, BigDecimal> netBalances = calculateNetBalances(expenses);
        List<ExpenseDto> topExpenses = topExpenses(expenses);

        return Map.of(
                "totalSpending", totalSpending,
                "netBalances", netBalances,
                "topExpenses", topExpenses
        );
    }

    private Event findById(Long id) {
        log.debug("[FIND_EVENT] Start getting event with id: {}", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));
    }

    private List<ExpenseSplit> buildSplits(ExpenseVo vo, Event event, Expense expense) {

        if (vo.getSplits() != null && !vo.getSplits().isEmpty()) {
            return vo.getSplits().stream()
                    .map(split -> {

                        Member member = memberRepository
                                .findById(split.getMemberId())
                                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Member not found"));

                        return ExpenseSplit.builder()
                                .expense(expense)
                                .member(member)
                                .shareAmount(split.getShareAmount())
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        List<Member> members = memberRepository.findByEventId(event.getId());
        if (members.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "ILLEGAL_ACCESS", "Member not access event");
        }

        BigDecimal equalShare = vo.getAmount().divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);


        return members.stream()
                .map(member ->
                        ExpenseSplit.builder()
                                .expense(expense)
                                .member(member)
                                .shareAmount(equalShare)
                                .build()
                )
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> calculateNetBalances(List<ExpenseDto> expenses) {

        Map<String, BigDecimal> paidTotal = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseDto::getPaidByName,
                        Collectors.reducing(BigDecimal.ZERO, ExpenseDto::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> owedTotal = expenses.stream()
                .flatMap(expense -> expense.getSplits().stream())
                .collect(Collectors.groupingBy(
                        SplitDto::getMemberName,
                        Collectors.reducing(BigDecimal.ZERO, SplitDto::getShareAmount, BigDecimal::add)
                ));

        return Stream.concat(paidTotal.keySet().stream(), owedTotal.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> paidTotal.getOrDefault(name, BigDecimal.ZERO)
                                .subtract(owedTotal.getOrDefault(name, BigDecimal.ZERO))
                ));
    }

    private List<ExpenseDto> topExpenses(List<ExpenseDto> expenses) {
        return expenses.stream().sorted(Comparator.comparing(ExpenseDto::getAmount).reversed()).limit(3).toList();
    }

    private BigDecimal totalEventSpending(List<ExpenseDto> expenses) {
        return expenses.stream().map(ExpenseDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
