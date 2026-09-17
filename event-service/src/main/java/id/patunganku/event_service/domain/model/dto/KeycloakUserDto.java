package id.patunganku.event_service.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserDto {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public String getName() {
        String name = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();

        return name.isEmpty() ? username : name;
    }
}