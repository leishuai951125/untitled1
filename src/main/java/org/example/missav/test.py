#!/Library/Frameworks/Python.framework/Versions/3.11/bin/python3

# import requests
import cloudscraper

# https://blog.csdn.net/CharlesSimonyi/article/details/123163034
scraper = cloudscraper.create_scraper(browser={'browser': 'firefox', 'platform': 'windows', 'mobile': False})
resp = scraper.get("https://missav.com/makers/Crystal-Eizou")
print(resp.text)
