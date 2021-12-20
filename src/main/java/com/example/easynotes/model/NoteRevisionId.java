package com.example.easynotes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class NoteRevisionId implements Serializable {

    @Column(name = "revised_note_id")
    private Long revisedNoteId;

    @Column(name = "revisor_id")
    private Long revisorId;

}
