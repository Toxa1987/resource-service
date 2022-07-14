package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("should return song by id=1")

    request {
        url "/resources/1"
        method GET()
    }
    response {
        status OK()
        headers {
            contentType("audio/mpeg")
        }
        body(
                fileAsBytes("file.mp3")
        )
    }
}