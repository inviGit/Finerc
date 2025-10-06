package com.invi.finerc.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Transaction Category Enum
enum class Category(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val colors: List<Color>,
    val keywords: List<String>
) {
    FOOD(
        "Food",
        Icons.Default.Restaurant,
        Color(0xFFEF4444),
        listOf(Color(0xFFEF4444), Color(0xFFF97316)), // Orange gradient
        listOf(
            "zomato",
            "swiggy",
            "dominos",
            "mcdonald",
            "kfc",
            "subway",
            "restaurant",
            "food",
            "cafe",
            "hotel",
            "pizza",
            "burger",
            "dining",
            "meal",
            "breakfast",
            "lunch",
            "dinner",
            "snacks",
            "bakery",
            "canteen",
            "mess"
        )
    ),

    SHOPPING(
        "Shopping",
        Icons.Default.ShoppingCart,
        Color(0xFF6366F1),
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), // Teal to blue gradient
        listOf(
            "amazon",
            "flipkart",
            "blinkit",
            "grofers",
            "zepto",
            "myntra",
            "bigbasket",
            "nykaa",
            "ajio",
            "bazaar",
            "mall",
            "store",
            "shopping",
            "market",
            "shop",
            "purchase",
            "buy",
            "retail",
            "supermarket",
            "grocery",
        )
    ),

    TRAVEL(
        "Transportation",
        Icons.Default.DirectionsCar,
        Color(0xFF00D4AA),
        listOf(Color(0xFF00D4AA), Color(0xFF00B894)), // Aqua to purple gradient
        listOf(
            "uber",
            "ola",
            "taxi",
            "fastag",
            "indigo",
            "metro",
            "spicejet",
            "vistara",
            "bus",
            "fuel",
            "petrol",
            "diesel",
            "auto",
            "rickshaw",
            "cab",
            "transport",
            "travel",
            "railway",
            "train",
            "flight",
            "parking",
            "toll"
        )
    ),

    ENTERTAINMENT(
        "Entertainment",
        Icons.Default.Movie,
        Color(0xFFEC4899),
        listOf(Color(0xFFEC4899), Color(0xFFF59E0B)), // Pink to blue gradient
        listOf(
            "netflix",
            "spotify",
            "prime",
            "paytm insider",
            "bookmyshow",
            "hotstar",
            "youtube",
            "pvr",
            "inox",
            "movie",
            "game",
            "entertainment",
            "cinema",
            "theatre",
            "subscription",
            "music",
            "gaming",
            "concert",
            "event"
        )
    ),

    UTILITIES(
        "Utilities",
        Icons.Default.Settings,
        Color(0xFF56CCF2),
        listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)), // Light blue gradient
        listOf(
            "electricity",
            "water",
            "gas",
            "internet",
            "mobile",
            "recharge",
            "bill",
            "utility",
            "broadband",
            "wifi",
            "airtel",
            "jio",
            "vodafone",
            "bsnl",
            "tata",
            "bescom",
            "kseb",
            "mseb",
            "postpaid",
            "prepaid",
            "dth"
        )
    ),

    HEALTHCARE(
        "Healthcare",
        Icons.Default.LocalHospital,
        Color(0xFF10B981),
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Mint to blue gradient
        listOf(
            "manipal",
            "apollo",
            "fortis",
            "medplus",
            "netmeds",
            "pharmeasy",
            "1mg",
            "medicine",
            "health",
            "hospital",
            "pharmacy",
            "medical",
            "doctor",
            "dental",
            "lab",
            "pathology",
            "checkup",
            "treatment",
            "clinic"
            )
    ),

    EDUCATION(
        "Education",
        Icons.Default.School,
        Color(0xFF3B82F6),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)), // Orange to yellow gradient
        listOf(
            "school",
            "college",
            "course",
            "book",
            "education",
            "fees",
            "tuition",
            "study",
            "learn",
            "university",
            "academy",
            "institute",
            "coaching",
            "byju",
            "unacademy",
            "vedantu",
            "library",
            "exam",
            "certification"
        )
    ),

    ATM_CASH(
        "ATM/Cash",
        Icons.Default.AccountBox,
        Color(0xFF757F9A),
        listOf(Color(0xFF757F9A), Color(0xFFD7DDE8)), // Gray gradient
        listOf("atm", "cash", "withdrawal", "withdraw", "cashout", "branch", "counter", "teller")
    ),

    INVESTMENT(
        "Investment",
        Icons.Default.TrendingUp,
        Color(0xFFF59E0B),
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Green to teal gradient
        listOf(
            "mutual",
            "fund",
            "sip",
            "investment",
            "stock",
            "share",
            "trading",
            "zerodha",
            "upstox",
            "groww",
            "paytm money",
            "angelone",
            "icicidirect",
            "hdfcsec",
            "equity",
            "bond",
            "fd",
            "rd",
            "ppf",
            "elss"
        )
    ),

    INSURANCE(
        "Insurance",
        Icons.Default.MedicalInformation,
        Color(0xFF30E8BF),
        listOf(Color(0xFF30E8BF), Color(0xFFFF8235)), // Teal to orange gradient
        listOf(
            "insurance",
            "policy",
            "premium",
            "lic",
            "bajaj",
            "hdfc life",
            "sbi life",
            "icici prudential",
            "max life",
            "health insurance",
            "car insurance",
            "bike insurance",
            "term insurance",
            "ulip"
        )
    ),

    TRANSFER(
        "Transfer",
        Icons.Default.Info,
        Color(0xFFB993D6),
        listOf(Color(0xFFB993D6), Color(0xFF8CA6DB)), // Purple gradient
        listOf(
            "transfer",
            "sent",
            "upi",
            "imps",
            "neft",
            "rtgs",
            "paytm",
            "gpay",
            "phonepe",
            "bhim",
            "amazon pay",
            "freecharge",
            "mobikwik",
            "airtel money",
            "jio money",
            "p2p",
            "wallet"
        )
    ),

    LOAN_EMI(
        "Loan/EMI",
        Icons.Default.Payment,
        Color(0xFF8B5CF6),
        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), // Red to orange gradient
        listOf(
            "emi",
            "loan",
            "credit",
            "installment",
            "bajaj finserv",
            "tata capital",
            "fullerton",
            "home loan",
            "personal loan",
            "car loan",
            "education loan",
            "repayment",
            "interest",
            "principal"
        )
    ),

    GOVERNMENT(
        "Government",
        Icons.Default.Place,
        Color(0xFF00C6FB),
        listOf(Color(0xFF00C6FB), Color(0xFF005BEA)), // Blue gradient
        listOf(
            "tax",
            "income tax",
            "gst",
            "government",
            "challan",
            "fine",
            "license",
            "passport",
            "pan",
            "aadhar",
            "registration",
            "municipal",
            "corporation",
            "panchayat",
            "court",
            "legal"
        )
    ),

    CHARITY(
        "Charity/Donation",
        Icons.Default.Star,
        Color(0xFFF7971E),
        listOf(Color(0xFFF7971E), Color(0xFFFFD200)), // Orange to yellow gradient
        listOf(
            "donation",
            "charity",
            "temple",
            "church",
            "mosque",
            "gurudwara",
            "religious",
            "ngo",
            "fund",
            "help",
            "support",
            "contribute",
            "give",
            "donate",
            "zakat",
            "tithe",
            "offering"
        )
    ),

    OTHERS(
        "Others",
        Icons.Default.MoreVert,
        Color(0xFFDD5E89),
        listOf(Color(0xFFDD5E89), Color(0xFFF7BB97)), // Pink gradient
        listOf()
    );

    companion object {
        fun fromKeyword(text: String): Category {
            val lowerText = text.lowercase()

            // Find the category with matching keywords
            for (category in Category.entries) {
                if (category != OTHERS && category.keywords.any { keyword ->
                        lowerText.contains(keyword.lowercase())
                    }) {
                    return category
                }
            }

            return OTHERS
        }

        fun fromDisplayName(text: String): Category {
            val lowerText = text.lowercase()

            // Find the category with matching keywords
            for (category in Category.entries) {
                if (category.displayName.lowercase().equals(lowerText)) {
                    return category
                }
            }

            return OTHERS
        }
    }
}

// Updated CategoryModel to work with enum
data class CategoryModel(
    val displayName: String,
    val colors: List<Color> = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
    val icon: ImageVector,
    val keywords: List<String>
) {
    companion object {
        fun fromCategory(category: Category): CategoryModel {
            return CategoryModel(
                displayName = category.displayName,
                colors = category.colors,
                icon = category.icon,
                keywords = category.keywords
            )
        }
    }
}

fun getCategoryByPlace(
    placeName: String,
    placeDescription: String = "",
    placeType: String = ""
): Category {
    val searchText = "$placeName $placeDescription $placeType".lowercase()

    // Score each category based on keyword matches
    val categoryScores = Category.values()
        .filter { it != Category.OTHERS } // Don't score OTHER category
        .map { category ->
            val score = category.keywords.count { keyword ->
                searchText.contains(keyword.lowercase())
            }
            category to score
        }
        .filter { it.second > 0 } // Only consider categories with matches

    // Return the category with the highest score, or OTHER if no matches
    return categoryScores.maxByOrNull { it.second }?.first ?: Category.OTHERS
}

//enum class CategoryModel(val displayName: String, val icon: ImageVector, val color: Color,
//    val places: List<String>) {
//    FOOD("Food", Icons.Default.Favorite, Color.Red, listOf("restaurant", "food", "zomato", "swiggy", "cafe", "hotel")),
//    SHOPPING("Shopping", Icons.Default.Favorite, Color.Yellow,
//        listOf("amazon", "flipkart", "mall", "store", "shopping")),
//    TRAVEL("Travel", Icons.Default.Favorite, Color.Blue,
//        listOf("uber", "ola", "taxi", "metro", "bus", "fuel", "petrol")),
//
//    ENTERTAINMENT("Entertainment", Icons.Default.Favorite, Color.Magenta,
//        listOf("movie", "netflix", "spotify", "game", "entertainment")),
//
//    UTILITIES("Utilities", Icons.Default.Favorite, Color.Magenta,
//        listOf("electricity", "water", "gas", "internet", "mobile", "recharge")),
//
//    HEALTHCARE("Healthcare", Icons.Default.Favorite, Color.Magenta,
//        listOf("hospital", "pharmacy", "medical", "doctor", "clinic")),
//
//    EDUCATION("Education", Icons.Default.Favorite, Color.Magenta,
//        listOf("school", "college", "course", "book", "education")),
//
//    ATM("ATM", Icons.Default.Favorite, Color.Magenta,
//        listOf("atm", "cash", "withdrawal")),
//
//    OTHER("Other", Icons.Default.Favorite, Color.Cyan,
//        listOf(""))
//}


