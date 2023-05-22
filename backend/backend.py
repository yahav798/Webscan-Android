from flask import Flask, request
from flask_cors import CORS
from Main import main


########################################### APP CONSTS ###########################################


app = Flask(__name__)
CORS(app)


########################################### APP ROUTES ###########################################

'''
backend route that checks the log in credentials
'''
@app.route("/hello")
def msg():
    url = request.args.get('url')

    return "Your input is: " + str(url)

'''
backend route that checks the log in credentials
'''
@app.route("/scan")
def start_scan():
    url = request.args.get('url')

    return str(main(url))






########################################### APP SOCKET ###########################################

if __name__ == "__main__":
    #socketio.run(app, debug=True)
    app.run(debug=True)

