package com.financetracker.app.category;

import com.financetracker.app.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "categories")
public class CategoryEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @DBRef
    private List<UserEntity> users;
}
