FROM python:3.11-slim

WORKDIR /app

COPY .env ./
COPY src/ ./src/
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "src/store_manager.py"]