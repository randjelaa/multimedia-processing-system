import { Component } from '@angular/core';

import { UploadService }
from '../../core/services/upload.service';

import { RouterLink }
from '@angular/router';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css'
})
export class UploadComponent {

  selectedFile!: File;

  constructor(
    private uploadService: UploadService
  ) {
  }

  onFileSelected(event: any) {

    this.selectedFile =
      event.target.files[0];
  }

  upload() {

    if (!this.selectedFile) {

      return;
    }

    this.uploadService
      .upload(this.selectedFile)
      .subscribe(response => {

        console.log(response);

        alert('Upload successful!');
      });
  }
}
