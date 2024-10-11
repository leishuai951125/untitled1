



exit 0
export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890
cookiePath="/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/cookie.txt"
baidu=https://www.baidu.com/
#curl -c $cookiePath -b $cookiePath $baidu
#exit 0



curl -c $cookiePath -b $cookiePath 'https://missav.com/makers/Crystal-Eizou2' \
  -H 'authority: missav.com' \
  -H 'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7' \
  -H 'accept-language: zh-CN,zh;q=0.9' \
  -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "macOS"' \
  -H 'sec-fetch-dest: document' \
  -H 'sec-fetch-mode: navigate' \
  -H 'sec-fetch-site: none' \
  -H 'sec-fetch-user: ?1' \
  -H 'upgrade-insecure-requests: 1' \
  -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
  --compressed

#curl --location 'https://missav.com/makers/Crystal-Eizou2'


#curl --location --request POST 'https://orion-http.gw.postman.co/v1/request' \
#--header 'authority: orion-http.gw.postman.co' \
#--header 'accept: */*' \
#--header 'accept-language: zh-CN,zh;q=0.9' \
#--header 'authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImJmMTllMTYxLWVkN2MtNDM0Yi04NTZkLTRhMjRlODVlZGZmZCIsInVzZXJJZCI6NzU4Mjk1MywidGVhbUlkIjowLCJpdiI6InRRWlEyLy85QnQ5dGZyNTZMdkgrYlE9PSIsImFsZ28iOiJhZXMtMTI4IiwiaWF0IjoxNjg4NDA2NzU0LCJleHAiOjE2ODg0MDg1NTR9.ZbDcw3uIUA8ymig0CTmcVoXJCBuT4sW_OkFFwmsaYh8' \
#--header 'content-length: 0' \
#--header 'origin: https://web.postman.co' \
#--header 'pm-h0: User-Agent=PostmanRuntime/7.32.3, Accept=*/*, Cache-Control=no-cache, Postman-Token=b8ee3e71-2646-4b61-8283-75b4d5ec4cf5, Host=missav.com, Accept-Encoding=gzip%2C deflate%2C br, Cookie=XSRF-TOKEN%3DeyJpdiI6ImQ4OUtrc3Y1aGhYYklVb2U1QUYvTmc9PSIsInZhbHVlIjoiT3dOOTljdUJmY2RpdldZRE1ycHRiU2NHb051SDNiZy9EcXNyRjVpWmhZWGJ4Z0xITDZ2RklwVHhLd0M5aWQrT0ZUUy9jcnhYTVhDcnlvTENRaUlUUGxxc0pWckZFNFJ6akRzNUhSc3VFVytNQmVzbzRzSGdTQUd0SWRSVjJIb3YiLCJtYWMiOiI1ODgyODVhMTI0OTk2NDk0MDU0Y2RlYjY0MjlhODMxNTZhNjlhNjAzYmI1MTlmM2IwODg5OGJiOWRiZmVmNGI3IiwidGFnIjoiIn0%253D; missav_session%3DeyJpdiI6InFMcFVuQWU4bktYZE1MTGlsdXBCVFE9PSIsInZhbHVlIjoiQzB2ZkVWKzlCRVRpV1Z0TUtzTmM5Z0pWZHRRMXdiQnZpSk5OcGZCT3VGbytnOVZxbnlFeUl0V1dxOE8wc2krZ2Q5OG9wMTlHV3lDYmQ0dkppWmhzR3M4MFJHWmFuUThyR0JHNUJTcmwrcnNWbEt1bHJXemtJazRyS05zK0sxMlYiLCJtYWMiOiJhOTNhNmViNjk5NjVjNzBiNTBhNWZjYmY3NDFhOGM3MTExOGQxMTk0NGQwMzk4MGRkZjA2NGMyNjYwOTMxZDY4IiwidGFnIjoiIn0%253D; xNE4i9Pa2ljHM62liJUKkGIdtMRKa4Fd3ebNugmp%3DeyJpdiI6InVBbzdUdnBVMjZtK1VRd1BacXN2YWc9PSIsInZhbHVlIjoib3FEeEhHRitjWTU1ayt0cG1URm1yT1g5QzhlU1AvWkNKMnkzM21zY3hxMjZGejk1eXFIeTBtT1cxOUdPNEZGckNHbk5vQkVBb2FpMUY2NHZCbmNHNTN6OVc3RWJGQXU3eEdrNmpaeGl4UTFiS3Y5WDF2SmRkTkVQR1ZQaU9rU0RRZ1JWMkZGSjdWcElSSm41cWZGWkk2dThCeFlsWGd3clYyNVA5dlg1R2N2QjUrMStzcGF0YXlQL3AwUFB6cG1YdlhmQ3Z0REtZaklWdmp5YmYxZUlKd2NVeFhXTDQwWjBYYWc2OW1jUldZd1QreklyMzVsZ1doamkrVTl2bUdVb0dNa2o5dzJzSDRzNXdNWEdPcmoxZDI2S2V6L1VsZWR3djcwbWRWSGpyQ0RLc29kdUg5cmk4bWowUDdvQjdXdnk2ZHFBd1U5Y0pndDZIQnhrMGZ6QU5lNjZpUE5kd0JNRGtsSkJCMEg1cGFYemZxL0hHQmRlUU5GeUkzZDVuQ0ZqRmdrMkZYQmdsZDhYVFBpbGJReWNBTUd1TGF6cjViNGIwUXA4UkdFVVhkbz0iLCJtYWMiOiJhOTAwNmZkYzZlZTcwYmRiOGEyZTYwMWNiZjYyOTkwMTA5NjdkMzI0YmJhYWRmZjY5NWFhMDAwYWJlNzVlNGFlIiwidGFnIjoiIn0%253D' \
#--header 'pm-o0: method=GET, timings=true, timeout=180000, rejectUnauthorized=false' \
#--header 'pm-u: https://missav.com/search/ddt-?sort=saved&filters=individual&page=22' \
#--header 'referer: https://web.postman.co/workspace/My-Workspace~12d126cd-821e-469b-8d19-97e314d25059/request/7582953-4cbbe974-29bf-4beb-8171-482999766ada' \
#--header 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
#--header 'sec-ch-ua-mobile: ?0' \
#--header 'sec-ch-ua-platform: "macOS"' \
#--header 'sec-fetch-dest: empty' \
#--header 'sec-fetch-mode: cors' \
#--header 'sec-fetch-site: same-site' \
#--header 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36'
