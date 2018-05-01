import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {SongComponent} from "../song/song.component";

@Component({
  selector: 'app-share',
  templateUrl: './share.component.html',
  styleUrls: ['./share.component.css']
})
export class ShareComponent implements OnInit {

  copied = '';

  constructor(private dialogRef: MatDialogRef<SongComponent>,
              @Inject(MAT_DIALOG_DATA) private data: any,) {
  }

  ngOnInit() {
    let copyText = document.getElementById("link");
    // noinspection TypeScriptUnresolvedFunction
    copyText.select();
  }

  copyLink() {
    let copyText = document.getElementById("link");
    // noinspection TypeScriptUnresolvedFunction
    copyText.select();
    document.execCommand("Copy");
    this.copied = 'Content copied to clipboard!';
  }

  copyEmbedded() {
    let copyText = document.getElementById("embedded");
    // noinspection TypeScriptUnresolvedFunction
    copyText.select();
    document.execCommand("Copy");
    this.copied = 'Content copied to clipboard!';
  }
}
