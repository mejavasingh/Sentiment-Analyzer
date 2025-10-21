package com.sementic.demo.controller;

import com.sementic.demo.model.AspectRating;
import com.sementic.demo.model.ReviewRequest;
import com.sementic.demo.service.ReviewAnalyzerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    public ReviewController(ReviewAnalyzerService processReview) {
        this.processReview = processReview;
    }

    private final ReviewAnalyzerService processReview;

    @PostMapping("/reviews")
    public ResponseEntity<List<AspectRating>> addReview(@RequestBody ReviewRequest request) {
        return new ResponseEntity<>(processReview.analyze(request.reviewTest()), HttpStatus.OK);
    }
}