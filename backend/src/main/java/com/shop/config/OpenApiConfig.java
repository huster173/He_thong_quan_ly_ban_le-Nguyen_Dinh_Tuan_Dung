package com.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadata cho trang Swagger UI (/swagger-ui.html).
 *
 * Thu tu tag khai bao o day chinh la thu tu hien tren trang: nhom "Don hang" chua
 * ba API cua de bai len dau, cac nhom phu xuong duoi.
 */
@Configuration
public class OpenApiConfig {

    public static final String TAG_ORDERS = "Don hang";
    public static final String TAG_PRODUCTS = "San pham";
    public static final String TAG_ADMIN = "Quan tri";

    @Bean
    public OpenAPI orderInventoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("He thong Don hang & Kho hang")
                        .version("1.0.0")
                        .description("""
                                Ba API cua de bai, body cua phan sau la body phan truoc cong them \
                                dung mot truong:

                                - PHAN 1 `POST /api/orders/subtotal` -- body `{ items }`
                                - PHAN 2 `POST /api/orders/promotion` -- body `{ items, couponCode }`
                                - PHAN 3 `POST /api/orders/confirm` -- body `{ items, couponCode, region }`

                                Moi endpoint co san cac vi du trong o **Examples**: chon mot vi du \
                                roi bam Execute, khong can tu go JSON.

                                Tien te tinh bang so nguyen `long`, don vi 1 VND. Loi tra ve deu \
                                cung dang `{ code, message, details }`.

                                Du lieu mau: P01 Ao thun 150.000 (clothing, kho 20), P02 Quan jean \
                                450.000 (clothing, kho 10), P03 Tai nghe 890.000 (electronics, kho 5), \
                                P04 Sac du phong 350.000 (electronics, kho 8)."""))
                .tags(List.of(
                        new Tag().name(TAG_ORDERS)
                                .description("Ba API cua de bai -- chay lan luot Phan 1, Phan 2, Phan 3"),
                        new Tag().name(TAG_PRODUCTS)
                                .description("Danh sach san pham kem ton kho hien tai"),
                        new Tag().name(TAG_ADMIN)
                                .description("Lich su don hang -- ngoai de bai, chi doc")));
    }
}
