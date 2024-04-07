package com.financetracker.app.user;

import com.financetracker.app.security.authorization.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    private List<Role> roles = new ArrayList<>();
}
