package com.example.notesapp.data.localbase.mapers

import com.example.notesapp.data.localbase.database.entitys.Notes
import com.example.notesapp.data.remotebase.database.model.RemoteNotes

object NotesMaper {

    fun fromRemote(remoteNotes: RemoteNotes): Notes {
        return Notes(
            id = remoteNotes.id,
            noteName = remoteNotes.noteName,
            noteSpecification = remoteNotes.noteSpecification,
            noteDate = remoteNotes.noteDate
        )
    }

    fun toRemote(notes: Notes): RemoteNotes {
        return RemoteNotes(
            id = notes.id,
            noteName = notes.noteName,
            noteSpecification = notes.noteSpecification,
            noteDate = notes.noteDate
        )
    }
}