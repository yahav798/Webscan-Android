import requests

def main(url: str):

    if requests.get(url):
        return "URL exists!"
    else:
        return "URL don't exists!"

if __name__ == "__main__":
    print(main("http://www.zixem.altervista.org/SQLi/level1.php?id=1"))
