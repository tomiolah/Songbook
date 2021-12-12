import { Component, OnInit } from '@angular/core';
import { UserDataService } from '../../services/user-data.service';
import { MatDialog, MatSnackBar } from '@angular/material';
import { AuthenticateComponent } from '../authenticate/authenticate.component';

@Component({
  selector: 'app-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.css']
})
export class ActivateComponent implements OnInit {

  resendActivationEmailEnabled = true;

  constructor(
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
  }

  resendActivationEmail() {
    this.resendActivationEmailEnabled = false;
    this.userDataService.resendActivation().subscribe(() => {
    }, (err) => {
      if (err.message === 'Unexpected token < in JSON at position 0') {
        this.openAuthenticateDialog();
      } else {
        console.log(err);
        this.snackBar.open(err._body, 'Close', {
          duration: 5000
        });
      }
    });
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.resendActivationEmail();
      }
    });
  }

}
