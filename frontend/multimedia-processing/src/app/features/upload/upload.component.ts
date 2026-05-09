import { Component } from '@angular/core';
import { UploadService } from '../../core/services/upload.service';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css',
})
export class UploadComponent {
  selectedFile!: File;

  jobType = 'THUMBNAIL';

  constructor(private uploadService: UploadService) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  upload() {
    if (!this.selectedFile) {
      return;
    }

    this.uploadService
      .upload(this.selectedFile, this.jobType)
      .subscribe((response) => {
        console.log(response);

        alert('Upload successful!');
      });
  }
}
