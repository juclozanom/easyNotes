package com.example.easynotes.service;

import com.example.easynotes.dto.*;
import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Note;
import com.example.easynotes.model.NoteRevision;
import com.example.easynotes.model.Thank;
import com.example.easynotes.model.User;
import com.example.easynotes.repository.NoteRepository;
import com.example.easynotes.repository.NoteRevisionRepository;
import com.example.easynotes.repository.UserRepository;
import com.example.easynotes.utils.ListMapper;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService implements INoteService {

    NoteRepository noteRepository;
    UserRepository userRepository;
    NoteRevisionRepository noteRevisionRepository;
    ModelMapper modelMapper;
    ListMapper listMapper;

    @Autowired
    NoteService(NoteRepository noteRepository,
                UserRepository userRepository,
                ModelMapper modelMapper,
                ListMapper listMapper,
                NoteRevisionRepository noteRevisionRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.listMapper = listMapper;
        this.noteRevisionRepository = noteRevisionRepository;

        //Converter used to retrieve cant of user's notes
        Converter<Set<Note>, Integer> notesToCantNotesConverter = new AbstractConverter<Set<Note>, Integer>() {
            @Override
            protected Integer convert(Set<Note> notes) {
                return notes.size();
            }
        };

        //Load converter to modelMapper used when we want convert from User to UserResponseWithCantNotesDTO
        modelMapper.typeMap(User.class, UserResponseWithCantNotesDTO.class).addMappings( (mapper) ->
                mapper.using(notesToCantNotesConverter)
                        .map(User::getAuthorNotes, UserResponseWithCantNotesDTO::setCantNotes)
        );


        //Converter used to retrieve cant of user's notes
        Converter<Note, Long> userToIdConverter = new AbstractConverter<Note, Long>() {
            @Override
            protected Long convert(Note note) {
                return note.getId();
            }
        };

        modelMapper.typeMap(Thank.class, ThankDTO.class).addMappings( (mapper) ->
                mapper.using(userToIdConverter)
                        .map(Thank::getNote, ThankDTO::setNoteId)
        );



        /*modelMapper.typeMap(NoteRequestDTO.class, Note.class).addMappings( (mapper) ->
                mapper.with( req -> new Date() ).map(Note::setCreatedAt));*/

        this.modelMapper = modelMapper;
    }

    @Override
    public List<NoteResponseWithAuthorDTO> getAllNotes() {
        List<Note> notes = noteRepository.findAll();
        return listMapper.mapList(notes, NoteResponseWithAuthorDTO.class);
    }

    @Override
    public NoteResponseWithAuthorDTO createNote(NoteRequestDTO noteRequestDTO) {
        // Create new note
        Note note = modelMapper.map(noteRequestDTO, Note.class);

        //!FIXME
        note.setCreatedAt(LocalDate.now());
        note.setUpdatedAt(LocalDate.now());


        Note noteReq = noteRepository.save(note);

        //FIXME
        //Long idNote = note.getId();

        NoteResponseWithAuthorDTO resp =  modelMapper.map(noteReq, NoteResponseWithAuthorDTO.class);
        return resp;
    }

    @Override
    public NoteResponseWithAuthorDTO getNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));
        return modelMapper.map(note, NoteResponseWithAuthorDTO.class);
    }

    @Override
    public NoteResponseWithAuthorDTO updateNote(Long noteId,
                                                Note noteDetailsDTO) {

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        note.setTitle(noteDetailsDTO.getTitle());
        note.setContent(noteDetailsDTO.getContent());
        note.setAuthor(noteDetailsDTO.getAuthor());

        Note updatedNote = noteRepository.save(note);
        return modelMapper.map(updatedNote, NoteResponseWithAuthorDTO.class);
    }

    @Override
    public void deleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        noteRepository.delete(note);
    }


    public void addReviser(Long id, Long authorId, String state) {

        User user = userRepository.findById(authorId).
                orElseThrow(  () -> new ResourceNotFoundException("User", "id", id) );

        Note note = noteRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Note", "id", id) );

        NoteRevision newData = new NoteRevision(note,user,state);



        noteRevisionRepository.save(newData);
    }

    @Override
    public Set<ThankDTO> getThanks(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Note", "id", id) );

        return listMapper.mapSet( note.getThanks(), ThankDTO.class );
    }

    public List<NoteResponseWithCantLikesDTO> getThreeMoreThankedNotes (int year){

        List<HashMap<String, Object>> notesMoreThanked = noteRepository.findTopThreeNotesMostThankedByDate(year);
        return notesMoreThanked.stream()
                .limit(3L)
                .map( m -> {
                    Long id = (Long) m.get("id");
                    Long cant = (Long) m.get("cant_thanks");
                    return new NoteResponseWithCantLikesDTO(id, Math.toIntExact(cant) );
                }
                )
                .collect(Collectors.toList());
    }

    @Override
    public NoteTypeDTO getNoteType(Long noteId) {
        NoteTypeDTO response = new NoteTypeDTO();
        Optional<Note> noteWrapper = noteRepository.findById(noteId);
        Note note = null;
        if (noteWrapper.isPresent()) note = noteWrapper.get();
        if (note == null) throw new ResourceNotFoundException("notes", "id", noteId);
        response.setId(note.getId());
        int likesAmount = note.getThanks().size();
        response.setLikesAmount(likesAmount);

        if (likesAmount >= 5 && likesAmount <= 10) {
            response.setType("DeInteres");
        } else if( likesAmount > 10 ) response.setType("Destacada");
        else response.setType("Normal");
        return response;
    }

    @Override
    public List<NoteDTO> getNotesByType(String type) {
        Map<String, List<Long>> dic = new HashMap<>();
        dic.put("DeInteres", Arrays.asList(5L, 10L));
        dic.put("Destacada", Arrays.asList(11L, Long.MAX_VALUE));
        dic.put("Normal", Arrays.asList(0L, 4L));

        //List<HashMap<String, Object>> res = noteRepository.getNotesByType(dic.get(type).get(0), dic.get(type).get(1));
        List<Long> res = noteRepository.getNotesByType(dic.get(type).get(0), dic.get(type).get(1));
        List<Note> notes = new ArrayList<>();
        res.stream().forEach((note) ->
                {
                    Optional<Note> note1 = this.noteRepository.findById(note);
                    note1.ifPresent(notes::add);
                }
        );
        return listMapper.mapList(notes, NoteDTO.class );
    }

    /**
     * metodo nuevo para actualizar las revisiones con un nuevo estado
     * @param id
     * @param authorId
     * @param state
     */
    @Override
    public void updateRevision(Long idRevisedNote, Long authorId, String state) {
        NoteRevision datoActual = noteRevisionRepository.findByPrimaryId(authorId,idRevisedNote);
        datoActual.setState(state);
        noteRevisionRepository.save(datoActual);
    }
}


