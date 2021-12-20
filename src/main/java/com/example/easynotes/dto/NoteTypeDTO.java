package com.example.easynotes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteTypeDTO {
    private String type;
    private Long id;
    private Integer likesAmount;
}
