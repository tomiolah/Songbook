import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-open-in-app',
  templateUrl: './open-in-app.component.html',
  styleUrls: ['./open-in-app.component.css']
})
export class OpenInAppComponent implements OnInit {

  constructor(private dialogRef: MatDialogRef<OpenInAppComponent>) {
  }

  ngOnInit() {
  }

  dontShowAgain() {
    localStorage.setItem("OpenInAppComponent_dontShow", "true");
    this.dialogRef.close('ok');
  }
}
