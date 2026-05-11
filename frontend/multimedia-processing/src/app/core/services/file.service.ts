import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class FileService {
  private api = 'http://localhost:8080/api/files';

  constructor(private http: HttpClient) {}

  upload(file: File, type: string) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return this.http.post(`${this.api}/upload`, formData);
  }

  download(jobId: string) {
    return this.http.get(`${this.api}/download/${jobId}`, {
      responseType: 'blob',
    });
  }
}
