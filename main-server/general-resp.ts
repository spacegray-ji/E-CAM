export interface GeneralResponse<T> {
  status: Status
  message: string
  data: T
  // Error
  isError: boolean
}

export enum Status {
  OK = 200,
  INVALID_REQUEST = 400,
  INVALID_PARAMETER = 401,
  NOT_FOUND = 404,
  INTERNAL_SERVER_ERROR = 500,
}