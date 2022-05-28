export interface GeneralResponse<T> {
  status: Status
  message: string
  data: T
  // Error
  isError: boolean
}

export enum Status {
  OK = 200,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  INTERNAL_SERVER_ERROR = 500,
}