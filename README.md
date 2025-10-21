Ever wondered what users love or hate about a product - not just whether they like it?

I built a lightweight Aspect-Based Sentiment Analysis (ABSA) engine in pure Java 8 that:
✅ Detects key aspects (e.g., camera, battery, display) from reviews
✅ Matches sentiment words ("awesome", "worse", "ok") to the correct aspect using linguistic proximity
✅ Outputs structured ratings (1–5 scale) per feature

🛠️ Tech: Java 8 Streams, HashMap optimizations, regex tokenization — no external NLP libraries!

Requeest: 
http://localhost:8080/api/reviews

{
  "reviewTest": "display is awesome, camera is good but battery drain fast , build quality is ok but battery is worse."
}


Result:

[
    {
        "aspect": "build",
        "sentimentScore": 0.3,
        "numericRating": 3.6
    },
    {
        "aspect": "display",
        "sentimentScore": 0.9,
        "numericRating": 4.8
    },
    {
        "aspect": "battery",
        "sentimentScore": -0.8,
        "numericRating": 1.4
    },
    {
        "aspect": "camera",
        "sentimentScore": 0.6,
        "numericRating": 4.2
    }
]
