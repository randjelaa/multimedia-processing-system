import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Job } from '../models/job.model';

@Injectable({
  providedIn: 'root',
})
export class JobService {
  private api = 'http://localhost:8080/api/jobs';

  constructor(private http: HttpClient) {}

  getJob(id: string): Observable<Job> {
    return this.http.get<Job>(`${this.api}/${id}`);
  }

  abort(jobId: string) {
    return this.http.post(`${this.api}/abort/${jobId}`, {});
  }
}
