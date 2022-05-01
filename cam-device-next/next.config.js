/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  compiler: {
    styledComponents: true,
  },
  rewrites: async () => {
    return [
      {
        source: "/local/:path*",
        destination: "http://localhost:7721/:path*",
      }
    ]
  }
}

module.exports = nextConfig
