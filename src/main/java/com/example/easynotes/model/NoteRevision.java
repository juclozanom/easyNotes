package com.example.easynotes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note_revision")
public class NoteRevision {

    @EmbeddedId
    NoteRevisionId id = new NoteRevisionId();

    @ManyToOne
    @MapsId("revisedNoteId")
    @JoinColumn(name = "revised_note_id")
    private Note note;

    @ManyToOne
    @MapsId("revisorId")
    @JoinColumn(name = "revisor_id")
    private User user;

    private String state;

    public NoteRevision(Note note, User user, String state) {
        this.note = note;
        this.user = user;
        this.state = state;
    }
}
