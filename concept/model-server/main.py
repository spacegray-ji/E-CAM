import os.path

from flask import Flask, render_template, url_for, flash, redirect, request, jsonify, Response
from werkzeug.utils import secure_filename
from PIL import Image
import imghdr

# https://flask.palletsprojects.com/en/2.0.x/patterns/fileuploads/
UPLOAD_FOLDER = "./uploads"
ALLOWED_EXTENSIONS = {"jpg"}

app = Flask(__name__)
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER


def allowed_file(filename: str):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/home")
def home():
    return "Hello, Flask HTTP Server!"


@app.route("/user/<user_name>/<int:user_id>")
def user(user_name: str, user_id: int):
    return f"Hello, {user_name} and {user_id}!"


@app.route("/process", methods=["POST"])
def process():
    input_files = request.files.getlist("images")
    print(len(input_files))
    result = {
        "error": False,
        "statusCode": 200,
        "message": "",
        "data": [],
    }
    for file in input_files:
        if file.filename == "":
            result["error"] = True
            result["statusCode"] = 403
            result["message"] = "File isn't presented."
            break
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            temp_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)
            file.save(temp_path)
            # Check is image
            if imghdr.what(temp_path) == "jpeg":
                # Image context
                img = Image.open(temp_path)

                result["data"].append({
                    "filename": filename,
                    "pixelSize": img.width * img.height,
                })
                # Close Image
                img.close()
                os.remove(temp_path)
                continue
            else:
                # No JPEG: Aborting!
                pass
        result["error"] = True
        result["statusCode"] = 400
        result["message"] = "Only JPG is allowed."
        break
    return jsonify(result), result["statusCode"]


if __name__ == "__main__":
    app.run(port=23579, debug=True)

