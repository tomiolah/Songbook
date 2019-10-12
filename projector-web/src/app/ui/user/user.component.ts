import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs/Subscription";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute } from "@angular/router";
import { User } from "../../models/user";
import { UserDataService } from "../../services/user-data.service";
import { DomSanitizer, SafeResourceUrl, Title } from "@angular/platform-browser";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { MatDialog } from "@angular/material";
import { Role, getAllRole } from '../../models/role';


@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit, OnDestroy {

  originalUser: User;
  user: User;
  public safeUrl: SafeResourceUrl = null;
  private sub: Subscription;
  roles: Role[];

  constructor(
    private activatedRoute: ActivatedRoute,
    private userService: UserDataService,
    private titleService: Title,
    public auth: AuthService,
    public sanitizer: DomSanitizer,
    private dialog: MatDialog) {
    auth.getUserFromLocalStorage();
    this.user = new User();
    this.user.email = "Loading";
    this.getRoles();
  }

  ngOnInit() {
    this.titleService.setTitle('User');

    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['userId']) {
        const userId = params['userId'];
        this.userService.getUser(userId).subscribe(
          (user) => {
            this.user = user;
            if (user.isAdmin()) {
              this.roles.push(Role.ROLE_ADMIN);
            }
            this.originalUser = new User(this.user);
          },
          (err) => {
            if (err.message === 'Unexpected token < in JSON at position 0') {
              this.openAuthenticateDialog();
            }
          });
      }
    });
  }

  private getRoles() {
    let roles = getAllRole();
    const index = roles.indexOf(Role.ROLE_ADMIN, 0);
    if (index > -1) {
      roles.splice(index, 1);
    }
    this.roles = roles;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  getRoleString(role: Role): string {
    return Role.getAsString(role);
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
        this.ngOnInit();
      }
    });
  }

  onApplyRoleButtonClick() {
    const updateUser = new User(this.originalUser);
    updateUser.role = this.user.role;
    this.userService.update(updateUser).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.originalUser = updateUser;
      },
      (err) => {
        if (err.status === 405) {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    );
  }
}
