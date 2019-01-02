import {Component, Inject, OnInit} from '@angular/core';
import {Song} from "../../services/song-service.service";
import {SongCollectionDataService} from "../../services/song-collection-data.service";
import {SongCollection, SongCollectionElement} from "../../models/songCollection";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-add-to-collection',
  templateUrl: './add-to-collection.component.html',
  styleUrls: ['./add-to-collection.component.css']
})
export class AddToCollectionComponent implements OnInit {

  form: FormGroup;
  song: Song;
  songCollections: SongCollection[];
  selectedSongCollection: SongCollection;

  constructor(
    public dialogRef: MatDialogRef<AddToCollectionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private songCollectionDataService: SongCollectionDataService,
  ) {
  }

  ngOnInit() {
    this.song = this.data.song;
    this.songCollections = JSON.parse(localStorage.getItem("songCollections"));
    this.songCollectionDataService.getAll().subscribe(songCollections => {
      songCollections.sort((songCollection1, songCollection2) => {
        if (songCollection1.modifiedDate < songCollection2.modifiedDate) {
          return 1;
        }
        if (songCollection1.modifiedDate > songCollection2.modifiedDate) {
          return -1;
        }
        return 0;
      });
      this.songCollections = songCollections;
      localStorage.setItem("songCollections", JSON.stringify(songCollections));
    });
    this.form = this.fb.group({
      'ordinalNumber': ['', [
        Validators.required,
      ]],
    });
  }

  onSubmit() {
    const formValue = this.form.value;
    let songCollectionElement: SongCollectionElement = new SongCollectionElement();
    songCollectionElement.ordinalNumber = formValue.ordinalNumber;
    songCollectionElement.songUuid = this.song.uuid;
    this.songCollectionDataService.putInCollection(this.selectedSongCollection, songCollectionElement).subscribe();
    this.dialogRef.close('ok');
  }
}
