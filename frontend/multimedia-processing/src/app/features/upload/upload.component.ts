import { Component, OnDestroy } from '@angular/core';
import { FileService } from '../../core/services/file.service';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../core/services/job.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css',
})
export class UploadComponent implements OnDestroy {
  selectedFile!: File;

  jobType = 'THUMBNAIL';

  currentJob: any = null;

  pollingSubscription?: Subscription;

  isUploading = false;

  constructor(
    private uploadService: FileService,

    private jobService: JobService,
  ) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  upload() {
    if (!this.selectedFile) {
      return;
    }

    this.isUploading = true;

    this.uploadService
      .upload(this.selectedFile, this.jobType)
      .subscribe((response: any) => {
        console.log(response);

        this.currentJob = response;

        this.startPolling(response.id);
      });
  }

  startPolling(jobId: string) {
    this.pollingSubscription = interval(2000)
      .pipe(switchMap(() => this.jobService.getJob(jobId)))
      .subscribe((job) => {
        console.log(job);

        this.currentJob = job;

        if (job.status === 'DONE' || job.status === 'FAILED') {
          this.pollingSubscription?.unsubscribe();

          this.isUploading = false;
        }
      });
  }

  getProgressWidth(): string {
    if (!this.currentJob) {
      return '0%';
    }

    return `${this.currentJob.progressPercentage}%`;
  }

  downloadFile() {
    if (!this.currentJob || this.currentJob.status !== 'DONE') return;

    this.uploadService.download(this.currentJob.id).subscribe({
      next: (blob: Blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.style.display = 'none';
        a.href = downloadUrl;

        let extension = '';
        switch (this.currentJob.type) {
          case 'THUMBNAIL':
            extension = '.jpg';
            break;
          case 'AUDIO':
            extension = '.mp3';
            break;
          case 'TRANSCODE':
            extension = '.mp4';
            break;
        }

        const baseName = this.currentJob.originalFileName.split('.')[0];
        a.download = `${baseName}_result${extension}`;

        a.click();

        setTimeout(() => {
          window.URL.revokeObjectURL(downloadUrl);
          document.body.removeChild(a);
        }, 100);
      },
    });
  }

  abortProcessing() {
    if (!this.currentJob) return;

    this.jobService.abort(this.currentJob.id).subscribe({
      next: () => {
        console.log('Abort request sent');
        this.pollingSubscription?.unsubscribe();
        this.currentJob.status = 'ABORTED';
        this.isUploading = false;
      },
      error: (err) => console.error('Failed to abort', err),
    });
  }

  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }
}
