package com.example.easynotes.repository;

import com.example.easynotes.model.NoteRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRevisionRepository extends JpaRepository<NoteRevision,Long> {

    @Query("FROM NoteRevision nr where nr.user.id = :idUser and nr.note.id = :idRevisedNote")
    NoteRevision findByPrimaryId(Long idUser,Long idRevisedNote);
}
