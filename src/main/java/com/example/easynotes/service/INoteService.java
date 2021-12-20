package com.example.easynotes.service;

import com.example.easynotes.dto.*;
import com.example.easynotes.model.Note;

import java.util.List;
import java.util.Set;

public interface INoteService {

    List<NoteResponseWithAuthorDTO> getAllNotes();

    NoteResponseWithAuthorDTO createNote(NoteRequestDTO noteRequestDTO);

    NoteResponseWithAuthorDTO getNoteById(Long noteId);

    NoteResponseWithAuthorDTO updateNote(Long noteId, Note noteDetailsDTO);

    void deleteNote(Long noteId);

    void addReviser(Long id, Long authorId, String state);

    Set<ThankDTO> getThanks(Long id);

    List<NoteResponseWithCantLikesDTO> getThreeMoreThankedNotes (int year);

    NoteTypeDTO getNoteType(Long noteId);

    List<NoteDTO> getNotesByType(String type);

    void updateRevision(Long idRevisedNote, Long authorId, String state);
}
