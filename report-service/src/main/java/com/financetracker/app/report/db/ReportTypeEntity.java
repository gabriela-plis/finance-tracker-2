package com.financetracker.app.report.db;

import com.financetracker.app.user.UserEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reports")
public class ReportTypeEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private ReportType name;

    @DBRef
    private List<UserEntity> subscribers;
}
