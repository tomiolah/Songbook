import { AuthenticateComponent } from '../ui/authenticate/authenticate.component';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogRef } from '@angular/material/dialog';

export class ErrorUtil {

  public static errorIsNeededLogin(err): Boolean {
    if (err.status == 405 || err.status == 401) {
      return true;
    }
    return err.message === 'Unexpected token < in JSON at position 0';
  }

  public static showError(err, snackBar: MatSnackBar) {
    console.log(err);
    snackBar.open(err._body, 'Close', {
      duration: 5000
    });
  }

}

class DialogUtil {
  openDialogTime
}

export function getAuthenticateDialogRef(dialog: MatDialog): MatDialogRef<AuthenticateComponent, any> {
  const user = JSON.parse(localStorage.getItem('currentUser'));
  if (user == null) {
    return;
  }
  return dialog.open(AuthenticateComponent, {
    data: {
      email: user.email
    }
  });
}

export function openAuthenticateDialog<T>(callbackFn: (this: T) => any, thisArg: T, dialog: MatDialog): any;
export function openAuthenticateDialog<T, TResult>(callbackFn: (this: T) => TResult, thisArg: T, dialog: MatDialog) {
  const dialogRef = getAuthenticateDialogRef(dialog);
  if (dialogRef == undefined) {
    window.location.href = '/#/login';
    return;
  }
  dialogRef.afterClosed().subscribe((result) => {
    if (result === 'ok') {
      return callbackFn.call(thisArg);
    }
  });
}

export function checkAuthenticationError<T>(callbackFn: (this: T) => any, thisArg: T, err, dialog: MatDialog): any;
export function checkAuthenticationError<T, TResult>(callbackFn: (this: T) => TResult, thisArg: T, err, dialog: MatDialog) {
  if (ErrorUtil.errorIsNeededLogin(err)) {
    openAuthenticateDialog(callbackFn, thisArg, dialog);
  }
}