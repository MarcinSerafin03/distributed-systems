from fastapi import FastAPI
from typing import List
from pydantic import BaseModel
from uuid import uuid4
from typing import Dict
from fastapi import HTTPException

db_polls: Dict[str, Dict] = {}

class Poll(BaseModel):
    question: str
    answers: List[str]



app = FastAPI()

@app.post("/polls/")
def create_poll(poll: Poll):
    poll_id = str(uuid4())
    db_polls[poll_id] = {
        "question": poll.question,
        "answers": {str(uuid4()): {"text": ans, "votes": 0} for ans in poll.answers}
    }
    return {"poll_id": poll_id, "poll": db_polls[poll_id]}

@app.get("/polls/{poll_id}")
def get_poll(poll_id: str):
    if poll_id not in db_polls:
        return HTTPException(status_code=404, detail="Poll not found")
    return {"poll": db_polls[poll_id]}

@app.delete("/polls/{poll_id}")
def delete_poll(poll_id: str):
    if poll_id not in db_polls:
        return HTTPException(status_code=404, detail="Poll not found")
    del db_polls[poll_id]
    return {"message": "Poll deleted"}

@app.post("/polls/{poll_id}/answer")
def answer_poll(poll_id: str, answer: str):
    if poll_id not in db_polls:
        return HTTPException(status_code=404, detail="Poll not found")
    if answer not in db_polls[poll_id]["answers"]:
        return HTTPException(status_code=400, detail="Invalid answer")
    db_polls[poll_id]["answers"][answer]["votes"] += 1
    return {"message": "Answer accepted"}

@app.get("/polls/{poll_id}/results")
def get_results(poll_id: str):
    if poll_id not in db_polls:
        return HTTPException(status_code=404, detail="Poll not found")
    return {"results": db_polls[poll_id]["answers"]}

@app.post("/polls/{poll_id}/update")
def update_poll(poll_id: str, poll: Poll):
    if poll_id not in db_polls:
        return HTTPException(status_code=404, detail="Poll not found")
    db_polls[poll_id] = {
        "question": poll.question,
        "answers": {str(uuid4()): {"text": ans, "votes": 0} for ans in poll.answers}
    }
    return {"message": "Poll updated"}