package id.patunganku.event_service.service.impl;

import id.patunganku.event_service.config.exception.CustomException;
import id.patunganku.event_service.domain.entity.Event;
import id.patunganku.event_service.domain.entity.Expense;
import id.patunganku.event_service.domain.entity.ExpenseSplit;
import id.patunganku.event_service.domain.entity.Member;
import id.patunganku.event_service.domain.entity.enumeric.EventStatus;
import id.patunganku.event_service.domain.model.dto.EventDto;
import id.patunganku.event_service.domain.model.dto.ExpenseDto;
import id.patunganku.event_service.domain.model.dto.MemberDto;
import id.patunganku.event_service.domain.model.dto.SplitDto;
import id.patunganku.event_service.domain.model.vo.EventVo;
import id.patunganku.event_service.domain.model.vo.ExpenseVo;
import id.patunganku.event_service.repository.EventRepository;
import id.patunganku.event_service.repository.ExpenseRepository;
import id.patunganku.event_service.repository.MemberRepository;
import id.patunganku.event_service.service.EventService;
import id.patunganku.event_service.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    private final KeycloakUserService keycloakUserService;

    @Override
    @Transactional
    public EventDto create(EventVo vo) {
        log.info("[CREATE_EVENT] Start. name={}, memberCount={}", vo.getName(), vo.getMemberUserIds().size());

        String currentUserId = getCurrentUserId();
        log.debug("[CREATE_EVENT] Current user={}", currentUserId);

        keycloakUserService.validateUser(currentUserId);

        Event event = Event.builder()
                .name(vo.getName())
                .status(EventStatus.ACTIVE)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.debug("[CREATE_EVENT] Event saved. eventId={}", savedEvent.getId());

        List<Member> members = vo.getMemberUserIds()
                .stream()
                .distinct()
                .peek(keycloakUserId -> {
                    log.debug("[CREATE_EVENT] Validate member. userId={}", keycloakUserId);
                    keycloakUserService.validateUser(keycloakUserId);
                })
                .map(keycloakUserId ->
                        Member.builder()
                                .event(savedEvent)
                                .keycloakUserId(keycloakUserId)
                                .build()
                )
                .collect(Collectors.toList());

        if (members.stream().noneMatch(member -> currentUserId.equals(member.getKeycloakUserId()))) {
            members.add(Member.builder()
                    .event(event)
                    .keycloakUserId(currentUserId)
                    .build());

            log.debug("[CREATE_EVENT] Current user added as member. eventId={}, userId={}", savedEvent.getId(), currentUserId);
        }

        List<Member> savedMembers = memberRepository.saveAll(members);
        log.info("[CREATE_EVENT] Success. eventId={}, memberCount={}", savedEvent.getId(), savedMembers.size());

        String createdByName = keycloakUserService.getUserName(event.getCreatedBy());

        List<MemberDto> memberDtos = toMemberDtos(members);

        return EventDto.from(event, memberDtos, createdByName);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getById(Long id) {
        log.info("[GET_EVENT] Start. eventId={}", id);

        Event event = findById(id);

        String createdByName = keycloakUserService.getUserName(event.getCreatedBy());
        List<MemberDto> memberDtos = toMemberDtos(memberRepository.findByEventId(id));
        log.info("[GET_EVENT] Success. eventId={}, memberCount={}", id, memberDtos.size());

        return EventDto.from(event, memberDtos, createdByName);
    }

    @Override
    @Transactional
    public ExpenseDto addExpense(Long id, ExpenseVo vo) {
        log.info("[ADD_EXPENSE] Start. eventId={}, description={}, amount={}", id, vo.getDescription(), vo.getAmount());

        Event event = findById(id);

        String currentUserId = getCurrentUserId();
        log.debug("[ADD_EXPENSE] Current user={}", currentUserId);

        Member paidBy = memberRepository.findByEventIdAndKeycloakUserId(id, currentUserId)
                .orElseThrow(() -> {
                    log.warn("[ADD_EXPENSE] Current user is not a member. eventId={}, userId={}", id, currentUserId);
                    return new CustomException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Current user is not a member of event");
                });

        log.debug("[ADD_EXPENSE] Payer found. eventId={}, memberId={}", id, paidBy.getId());

        Expense expense = Expense.builder()
                .event(event)
                .paidBy(paidBy)
                .description(vo.getDescription())
                .amount(vo.getAmount())
                .build();

        List<ExpenseSplit> splits = buildSplits(vo, event, expense);
        log.debug("[ADD_EXPENSE] Splits built. eventId={}, splitCount={}", id, splits.size());

        validateSplitAmount(vo.getAmount(), splits);
        expense.setSplits(splits);

        Expense savedExpense = expenseRepository.save(expense);
        log.info("[ADD_EXPENSE] Success. expenseId={}, eventId={}, paidByMemberId={}, splitCount={}, amount={}",
                savedExpense.getId(), id, paidBy.getId(), splits.size(), savedExpense.getAmount());

        return toExpenseDto(savedExpense);
    }

    @Override
    public List<ExpenseDto> getExpenses(Long eventId) {
        log.info("[GET_EXPENSES] Start. eventId={}", eventId);

        findById(eventId);

        List<Expense> expenses = expenseRepository.findByEventId(eventId);
        log.debug("[GET_EXPENSES] Expenses loaded. eventId={}, expenseCount={}", eventId, expenses.size());

        List<ExpenseDto> result = expenses.stream()
                .map(expense -> {
                    log.debug("[GET_EXPENSES] Mapping expense. eventId={}, expenseId={}, amount={}", eventId, expense.getId(),
                            expense.getAmount());
                    return toExpenseDto(expense);
                })
                .collect(Collectors.toList());
        log.info("[GET_EXPENSES] Success. eventId={}, expenseCount={}", eventId, result.size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(Long id) {
        log.info("[GET_SUMMARY] Start. eventId={}", id);

        findById(id);

        List<ExpenseDto> expenses = expenseRepository.findByEventId(id)
                .stream()
                .map(this::toExpenseDto)
                .collect(Collectors.toList());

        log.debug("[GET_SUMMARY] Expense loaded. eventId={}, expenseCount={}", id, expenses.size());

        BigDecimal totalSpending = totalEventSpending(expenses);
        log.debug("[GET_SUMMARY] Total spending. eventId={}, total={}", id, totalSpending);

        Map<String, BigDecimal> netBalances = calculateNetBalances(expenses);
        log.debug("[GET_SUMMARY] Net balance calculated. eventId={}, userCount={}", id, netBalances.size());

        List<ExpenseDto> topExpenses = topExpenses(expenses);
        log.info("[GET_SUMMARY] Success. eventId={}, expenseCount={}, totalSpending={}", id, expenses.size(), totalSpending);

        return Map.of(
                "totalSpending", totalSpending,
                "netBalances", netBalances,
                "topExpenses", topExpenses
        );
    }

    private Event findById(Long id) {
        log.info("[FIND_EVENT] Start. eventId={}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[FIND_EVENT] Event not found. eventId={}", id);
                    return new CustomException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
                });

        log.debug("[FIND_EVENT] Success. eventId={}, status={}", event.getId(), event.getStatus());

        return event;
    }

    private List<ExpenseSplit> buildSplits(ExpenseVo vo, Event event, Expense expense) {

        if (vo.getSplits() != null && !vo.getSplits().isEmpty()) {
            log.debug("[BUILD_SPLITS] Manual split. eventId={}, requestedSplitCount={}", event.getId(), vo.getSplits().size());

            return vo.getSplits().stream()
                    .map(split -> {
                        Member member = memberRepository.findByIdAndEventId(split.getMemberId(), event.getId())
                                .orElseThrow(() -> {
                                    log.warn("[BUILD_SPLITS] Member not found. eventId={}, memberId={}", event.getId(), split.getMemberId());
                                    return new CustomException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Member not found in event");
                                });

                        log.debug("[BUILD_SPLITS] 1. Split added. eventId={}, memberId={}, shareAmount={}", event.getId(), member.getId(), split.getShareAmount());

                        return ExpenseSplit.builder()
                                .expense(expense)
                                .member(member)
                                .shareAmount(split.getShareAmount())
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        log.debug("[BUILD_SPLITS] Auto split. eventId={}, amount={}", event.getId(), vo.getAmount());

        List<Member> members = memberRepository.findByEventId(event.getId());
        log.debug("[BUILD_SPLITS] Members loaded. eventId={}, memberCount={}", event.getId(), members.size());

        if (members.isEmpty()) {
            log.warn("[BUILD_SPLITS] Event has no members. eventId={}", event.getId());
            throw new CustomException(HttpStatus.BAD_REQUEST, "MEMBER_NOT_FOUND", "Event has no members");
        }

        BigDecimal equalShare = vo.getAmount().divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
        log.debug("[BUILD_SPLITS] Equal share calculated. eventId={}, equalShare={}", event.getId(), equalShare);

        return members.stream()
                .map(member -> {
                    log.debug("[BUILD_SPLITS] 2. Split added. eventId={}, memberId={}, shareAmount={}", event.getId(), member.getId(), equalShare);

                    return ExpenseSplit.builder()
                            .expense(expense)
                            .member(member)
                            .shareAmount(equalShare)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void validateSplitAmount(BigDecimal amount, List<ExpenseSplit> splits) {
        BigDecimal totalSplit = splits.stream()
                .map(ExpenseSplit::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add
                );

        log.debug("[VALIDATE_SPLIT] Result. amount={}, totalSplit={}", amount, totalSplit);

        if (totalSplit.compareTo(amount) != 0) {
            log.warn("[VALIDATE_SPLIT] Invalid amount. amount={}, totalSplit={}", amount, totalSplit);
            throw new CustomException(HttpStatus.BAD_REQUEST, "INVALID_SPLIT_AMOUNT", "Total split amount must equal expense amount");
        }
    }

    private Map<String, BigDecimal> calculateNetBalances(List<ExpenseDto> expenses) {
        log.debug("[CALCULATE_BALANCES] Start. expenseCount={}", expenses.size());

        Map<String, BigDecimal> paidTotal = expenses.stream().
                collect(Collectors.groupingBy(ExpenseDto::getPaidByUserId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ExpenseDto::getAmount,
                                BigDecimal::add
                        )
                ));

        Map<String, BigDecimal> owedTotal = expenses.stream()
                .flatMap(expense ->
                        expense.getSplits().stream()
                )
                .collect(Collectors.groupingBy(SplitDto::getMemberUserId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                SplitDto::getShareAmount,
                                BigDecimal::add
                        )
                ));

        Map<String, BigDecimal> result = Stream.concat(paidTotal.keySet().stream(), owedTotal.keySet().stream())
                .distinct().collect(Collectors.toMap(userId -> userId, userId -> paidTotal.getOrDefault(userId, BigDecimal.ZERO)
                        .subtract(owedTotal.getOrDefault(userId, BigDecimal.ZERO))));

        log.debug("[CALCULATE_BALANCES] Completed. userCount={}", result.size());

        return result;
    }

    private List<ExpenseDto> topExpenses(List<ExpenseDto> expenses) {
        List<ExpenseDto> result = expenses.stream()
                .sorted(Comparator.comparing(ExpenseDto::getAmount).reversed())
                .limit(3)
                .collect(Collectors.toList());

        log.debug("[TOP_EXPENSES] Completed. sourceCount={}, resultCount={}", expenses.size(), result.size());

        return result;
    }

    private BigDecimal totalEventSpending(List<ExpenseDto> expenses) {
        BigDecimal total = expenses.stream()
                .map(ExpenseDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("[TOTAL_SPENDING] Calculated. expenseCount={}, total={}", expenses.size(), total);

        return total;
    }

    private String getCurrentUserId() {
        log.debug("[CURRENT_USER] Getting current user from JWT");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String userId = jwtAuthenticationToken.getToken().getSubject();
            if (userId != null && !userId.trim().isEmpty()) {
                log.debug("[CURRENT_USER] User resolved. userId={}", userId);
                return userId;
            }
        }

        log.warn("[CURRENT_USER] Authentication/JWT user not found");
        throw new CustomException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized");
    }

    private ExpenseDto toExpenseDto(Expense expense) {
        String paidByUserId = expense.getPaidBy().getKeycloakUserId();
        String paidByName = keycloakUserService.getUserName(paidByUserId);

        return ExpenseDto.builder()
                .id(expense.getId())
                .paidByMemberId(expense.getPaidBy().getId())
                .paidByUserId(expense.getPaidBy().getKeycloakUserId())
                .paidByName(paidByName)
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .splits(expense.getSplits()
                        .stream()
                        .map(split -> SplitDto.from(split, keycloakUserService.getUserName(split.getMember().getKeycloakUserId())))
                        .collect(Collectors.toList())
                )
                .build();
    }

    private MemberDto toMemberDto(Member member) {

        String name = keycloakUserService.getUserName(member.getKeycloakUserId());
        return MemberDto.builder()
                .id(member.getId())
                .keycloakUserId(member.getKeycloakUserId())
                .name(name)
                .build();
    }

    private List<MemberDto> toMemberDtos(List<Member> members) {
        return members.stream()
                .map(this::toMemberDto)
                .collect(Collectors.toList());
    }
}