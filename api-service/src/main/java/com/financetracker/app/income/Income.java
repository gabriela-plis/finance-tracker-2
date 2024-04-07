package com.financetracker.app.income;

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
@Document(collection = "incomes")
public class Income {
    @Id
    private String id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Field(targetType = DECIMAL128)
    private BigDecimal amount;

    private String description;

    @DBRef
    private User user;
}
