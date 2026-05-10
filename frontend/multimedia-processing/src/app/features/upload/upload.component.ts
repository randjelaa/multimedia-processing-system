import { Component, OnDestroy,} from '@angular/core';
import { UploadService } from '../../core/services/upload.service';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../core/services/job.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-upload',

  standalone: true,

  imports: [
    RouterLink,
    FormsModule,
    CommonModule,
  ],

  templateUrl:
    './upload.component.html',

  styleUrl:
    './upload.component.css',
})
export class UploadComponent
  implements OnDestroy
{
  selectedFile!: File;

  jobType = 'THUMBNAIL';

  currentJob: any = null;

  pollingSubscription?: Subscription;

  isUploading = false;

  constructor(
    private uploadService: UploadService,

    private jobService: JobService
  ) {}

  onFileSelected(event: any) {
    this.selectedFile =
      event.target.files[0];
  }

  upload() {
    if (!this.selectedFile) {
      return;
    }

    this.isUploading = true;

    this.uploadService
      .upload(
        this.selectedFile,
        this.jobType
      )
      .subscribe((response: any) => {
        console.log(response);

        this.currentJob = response;

        this.startPolling(
          response.id
        );
      });
  }

  startPolling(jobId: string) {
    this.pollingSubscription =
      interval(2000)
        .pipe(
          switchMap(() =>
            this.jobService.getJob(
              jobId
            )
          )
        )
        .subscribe((job) => {
          console.log(job);

          this.currentJob = job;

          if (
            job.status === 'DONE' ||
            job.status === 'FAILED'
          ) {
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

  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }
}