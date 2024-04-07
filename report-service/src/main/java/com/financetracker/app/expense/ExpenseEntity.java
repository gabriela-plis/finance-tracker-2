package com.financetracker.app.expense;

import com.financetracker.app.category.CategoryEntity;
import com.financetracker.app.user.UserEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "expenses")
public class ExpenseEntity {
    @Id
    private String id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @DBRef
    private CategoryEntity category;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @DBRef
    private UserEntity user;
}
