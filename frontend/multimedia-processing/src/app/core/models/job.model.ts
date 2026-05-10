export interface Job {
  id: string;
  originalFileName: string;
  objectKey: string;
  status: string;
  progressPercentage: number;
  resultFileKey: string;
  createdAt: string;
  finishedAt: string;
  type: string;
}