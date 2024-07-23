FROM python:3.8-slim

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Expose port 5000 to the outside world
EXPOSE 5000

# Install any needed packages specified in requirements.txt
RUN pip install -r requirements.txt

# Define the command to run the application
CMD ["python", "flask_api.py"]
