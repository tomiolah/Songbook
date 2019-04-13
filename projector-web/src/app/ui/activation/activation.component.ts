import { Component, OnInit, OnDestroy } from '@angular/core';
import { UserDataService } from '../../services/user-data.service';
import { MatSnackBar, MatDialog } from '@angular/material';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from "rxjs/Subscription";
import { AuthenticateComponent } from '../authenticate/authenticate.component';

@Component({
  selector: 'app-activation',
  templateUrl: './activation.component.html',
  styleUrls: ['./activation.component.css']
})
export class ActivationComponent implements OnInit, OnDestroy {

  activated = 'Something wrong';
  private sub: Subscription;
  code: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private userDataService: UserDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['code']) {
        this.code = params['code'];
        this.activate();
      }
    });
  }
  private activate() {
    this.userDataService.activate(this.code).subscribe(() => {
      this.activated = 'Successfully activated';
    }, (err) => {
      if (err.message === 'Unexpected token < in JSON at position 0') {
        this.openAuthenticateDialog();
      }
      else {
        console.log(err);
        this.snackBar.open(err._body, 'Close', {
          duration: 5000
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
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
        this.activate();
      }
    });
  }

}
