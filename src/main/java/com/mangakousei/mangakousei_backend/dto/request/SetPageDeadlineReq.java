package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SetPageDeadlineReq {
    @NotNull @Positive
    private Integer pageFrom;

    @NotNull @Positive
    private Integer pageTo;

    @NotNull @FutureOrPresent
    private LocalDate dueDate;
}
 
