import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-menu-tabs',
  templateUrl: './menu-tabs.component.html',
  styleUrls: ['./menu-tabs.component.css']
})
export class MenuTabsComponent implements OnInit {

  menuTabs = [
    {link: '/songs', icon: 'menu', title: 'Songs'},
    {link: '/addNewSong', icon: 'add_box', title: 'Add song'},
  ];

  constructor() {
  }

  ngOnInit() {
  }

}
