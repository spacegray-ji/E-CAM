async function main() {
  const test = (await import("got"))
  console.log(await test.got("https://example.org"))
}

main()