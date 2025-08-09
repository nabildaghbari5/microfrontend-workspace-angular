//package com.talentcloud.profile.lambda;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.core.SdkBytes;
//import software.amazon.awssdk.core.exception.SdkClientException;
//import software.amazon.awssdk.services.lambda.LambdaClient;
//import software.amazon.awssdk.services.lambda.model.*;
//
//@Service
//@Slf4j
//public class LambdaInvokerService {
//
//    private final LambdaClient lambdaClient;
//
//    public LambdaInvokerService(LambdaClient lambdaClient) {
//        this.lambdaClient = lambdaClient;
//    }
//
////    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
//    public String invokePythonScript(String inputJson) {
//        try {
//            InvokeRequest request = InvokeRequest.builder()
//                    .functionName("your-lambda-function-name")
//                    .payload(SdkBytes.fromUtf8String(inputJson))
//                    .build();
//
//            InvokeResponse response = lambdaClient.invoke(request);
//
//            if (response.statusCode() != 200) {
//                log.error("Lambda returned non-200 status: {}", response.statusCode());
//                throw new RuntimeException("Lambda failed with status: " + response.statusCode());
//            }
//
//            return response.payload().asUtf8String();
//
//        } catch (ResourceNotFoundException e) {
//            log.error("Lambda function not found", e);
//            throw new RuntimeException("Lambda function not found");
//        } catch (ServiceException e) {
//            log.error("ServiceException during Lambda invoke", e);
//            throw new RuntimeException("AWS Lambda Service error");
//        } catch (SdkClientException e) {
//            log.error("SdkClientException: AWS SDK communication issue", e);
//            throw new RuntimeException("AWS SDK error");
//        } catch (Exception e) {
//            log.error("General exception when calling Lambda", e);
//            throw new RuntimeException("Unexpected error calling Lambda");
//        }
//    }
//}
