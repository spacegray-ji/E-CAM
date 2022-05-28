// Next.js API route support: https://nextjs.org/docs/api-routes/introduction
import type { NextApiRequest, NextApiResponse } from "next"
import { cameraName } from "../../../libs/struct/conf_server"

type Data = {
  name: string,
  useLocalCam: boolean,
}
/* Deprecation */
export default function handler(
  req: NextApiRequest,
  res: NextApiResponse<Data>
) {
  res.status(200).json({ name: cameraName, useLocalCam: false })
}