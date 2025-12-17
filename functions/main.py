from firebase_functions import firestore_fn
from firebase_admin import initialize_app, messaging

initialize_app()

@firestore_fn.on_document_created(document="donations/{donationId}")
def send_new_donation_notification(event: firestore_fn.Event[firestore_fn.DocumentSnapshot]) -> None:
    # Get the data from the document
    data = event.data.to_dict()
    if not data:
        return

    title = data.get("title", "New Donation")
    
    # Send FCM message to topic "donations"
    message = messaging.Message(
        notification=messaging.Notification(
            title="New Food Available!",
            body=f"New Food: {title}"
        ),
        topic="donations"
    )
    
    response = messaging.send(message)
    print("Successfully sent message:", response)