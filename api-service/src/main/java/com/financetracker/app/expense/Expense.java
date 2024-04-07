package com.financetracker.app.expense;

import com.financetracker.app.category.Category;
import com.financetracker.app.user.User;
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
public class Expense {
    @Id
    private String id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @DBRef
    private Category category;

    @Field(targetType = DECIMAL128)
    private BigDecimal price;

    @DBRef
    private User user;
}
