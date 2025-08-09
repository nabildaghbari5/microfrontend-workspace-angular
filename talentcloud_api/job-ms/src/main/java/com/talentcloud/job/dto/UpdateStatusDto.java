package com.talentcloud.job.dto;

import com.talentcloud.job.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusDto {
    private Status status;
}