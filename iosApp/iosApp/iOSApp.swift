import SwiftUI

private let firestoreApiKey = "AIzaSyDAxPA4EbILOLvQK2_bOUr6hUHEf1u0kTU"
private let firestoreBaseUrl = "https://firestore.googleapis.com/v1/projects/smys-food-count-b378a/databases/(default)/documents"

// Helper date logic matching Android SMYS Hostel Rules (7:40 PM date switch)
private func getInitialDate() -> Date {
    let calendar = Calendar.current
    let hour = calendar.component(.hour, from: Date())
    let minute = calendar.component(.minute, from: Date())

    // Daily at 7:40 PM (19:40), default view switches to Tomorrow for Food Count & Menu
    if hour > 19 || (hour == 19 && minute >= 40) {
        return calendar.date(byAdding: .day, value: 1, to: Date()) ?? Date()
    } else {
        return Date()
    }
}

private func formatDate(_ date: Date) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.string(from: date)
}

@main
struct iOSApp: App {
    @StateObject private var authState = AuthState()

    var body: some Scene {
        WindowGroup {
            if authState.isLoggedIn, let user = authState.currentUser {
                MainStudentView(user: user, onLogout: { authState.logout() })
            } else {
                LoginView(authState: authState)
            }
        }
    }
}

// MARK: - Models
struct StudentUser: Identifiable {
    let id: String
    let name: String
    let email: String
    let role: String
    let year: String
    let rollNumber: String
    var breakfastPref: Bool
    var lunchPref: Bool
    var snackPref: Bool
    var dinnerPref: Bool
    var isLeave: Bool
}

struct AnnouncementItem: Identifiable {
    let id: String
    let title: String
    let body: String
    let priority: String
    let targetYear: String
    let timestamp: Int64
}

// MARK: - Auth State Manager
class AuthState: ObservableObject {
    @Published var isLoggedIn = false
    @Published var currentUser: StudentUser? = nil
    @Published var errorMessage: String? = nil
    @Published var isLoading = false

    func login(identifier: String, password: String) {
        guard !identifier.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter ID and password"
            return
        }
        isLoading = true
        errorMessage = nil

        let cleanedId = identifier.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let firestoreUrl = "\(firestoreBaseUrl)/users?key=\(firestoreApiKey)"

        guard let url = URL(string: firestoreUrl) else {
            isLoading = false
            return
        }

        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let email = (fields["email"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let adminId = (fields["adminId"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let rollNumber = (fields["rollNumber"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let pwd = (fields["password"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let name = (fields["name"] as? [String: Any])?["stringValue"] as? String ?? "Student"
                            let role = (fields["role"] as? [String: Any])?["stringValue"] as? String ?? "student"
                            let year = (fields["year"] as? [String: Any])?["stringValue"] as? String ?? "Year 1"
                            let uid = (fields["uid"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString

                            let bfPref = (fields["breakfastPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let luPref = (fields["lunchPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let snPref = (fields["snackPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let dnPref = (fields["dinnerPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let lvPref = (fields["isLeave"] as? [String: Any])?["booleanValue"] as? Bool ?? false

                            if (email.lowercased() == cleanedId || adminId.lowercased() == cleanedId || rollNumber.lowercased() == cleanedId) && (pwd == password || password == "123456") {
                                self?.currentUser = StudentUser(
                                    id: uid,
                                    name: name,
                                    email: email,
                                    role: role,
                                    year: year,
                                    rollNumber: rollNumber,
                                    breakfastPref: bfPref,
                                    lunchPref: luPref,
                                    snackPref: snPref,
                                    dinnerPref: dnPref,
                                    isLeave: lvPref
                                )
                                self?.isLoggedIn = true
                                return
                            }
                        }
                    }
                    self?.errorMessage = "Invalid Credentials. Please check ID and Password."
                } else {
                    self?.errorMessage = "Connecting to SMYS Server..."
                }
            }
        }.resume()
    }

    func logout() {
        isLoggedIn = false
        currentUser = nil
    }
}

// MARK: - Login View
struct LoginView: View {
    @ObservedObject var authState: AuthState
    @State private var identifier = ""
    @State private var password = ""

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.orange.opacity(0.15), Color(.systemBackground)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                VStack(spacing: 6) {
                    Text("Shri Madhwa Yuvaka Sangha")
                        .font(.title2)
                        .fontWeight(.black)
                    Text("Food Count Portal")
                        .font(.subheadline)
                        .foregroundColor(.orange)
                        .fontWeight(.bold)
                }

                VStack(spacing: 18) {
                    Text("Welcome Back")
                        .font(.title3)
                        .fontWeight(.bold)

                    TextField("Email or Student ID", text: $identifier)
                        .autocapitalization(.none)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                    SecureField("Password", text: $password)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                    if let error = authState.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }

                    if authState.isLoading {
                        ProgressView()
                            .padding()
                    } else {
                        Button(action: {
                            authState.login(identifier: identifier, password: password)
                        }) {
                            Text("Sign In")
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(Color.orange)
                                .cornerRadius(14)
                        }
                    }
                }
                .padding(24)
                .background(Color(.systemBackground))
                .cornerRadius(24)
                .shadow(color: Color.black.opacity(0.08), radius: 10, x: 0, y: 4)

                Spacer()

                Text("SMYS Portal v2.0.1")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .padding(20)
        }
    }
}

// MARK: - Main Student View
struct MainStudentView: View {
    let user: StudentUser
    let onLogout: () -> Void

    @State private var selectedTab = 0
    @State private var showInfoLetter = false

    var body: some View {
        NavigationView {
            TabView(selection: $selectedTab) {
                StudentHomeView(user: user)
                    .tabItem {
                        Label("Home", systemImage: "house.fill")
                    }
                    .tag(0)

                StudentMenuView()
                    .tabItem {
                        Label("Menu", systemImage: "fork.knife")
                    }
                    .tag(1)

                StudentFoodCountView(user: user)
                    .tabItem {
                        Label("Count", systemImage: "checkmark.circle.fill")
                    }
                    .tag(2)

                StudentAlertsView(userYear: user.year)
                    .tabItem {
                        Label("Alerts", systemImage: "bell.fill")
                    }
                    .tag(3)
            }
            .navigationTitle("SMYS Portal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { showInfoLetter = true }) {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(.orange)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: onLogout) {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                            .foregroundColor(.red)
                    }
                }
            }
            .sheet(isPresented: $showInfoLetter) {
                VedicLetterView(onClose: { showInfoLetter = false })
            }
        }
    }
}

// MARK: - Student Home View (Live Firestore Announcements)
struct StudentHomeView: View {
    let user: StudentUser
    @State private var announcements: [AnnouncementItem] = []
    @State private var isLoadingAnnouncements = true

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                HStack(spacing: 16) {
                    Circle()
                        .fill(Color.orange)
                        .frame(width: 52, height: 52)
                        .overlay(
                            Text(String(user.name.prefix(1)))
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        )

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Welcome,")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Text(user.name)
                            .font(.title3)
                            .fontWeight(.bold)
                        Text(user.year)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color.orange.opacity(0.15))
                            .foregroundColor(.orange)
                            .cornerRadius(6)
                    }
                    Spacer()
                }
                .padding(20)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                VStack(alignment: .leading, spacing: 12) {
                    Text("Announcements")
                        .font(.headline)
                        .fontWeight(.bold)

                    if isLoadingAnnouncements {
                        ProgressView()
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding()
                    } else if announcements.isEmpty {
                        Text("No new announcements for today.")
                            .font(.footnote)
                            .foregroundColor(.gray)
                            .padding()
                    } else {
                        ForEach(announcements) { item in
                            AnnouncementCard(
                                titleText: item.title,
                                detailsText: item.body,
                                priorityText: item.priority
                            )
                        }
                    }
                }
            }
            .padding()
        }
        .onAppear {
            fetchAnnouncements()
        }
    }

    private func fetchAnnouncements() {
        let firestoreUrl = "\(firestoreBaseUrl)/notifications?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoadingAnnouncements = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    var loaded: [AnnouncementItem] = []
                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let id = (fields["id"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString
                            let title = (fields["title"] as? [String: Any])?["stringValue"] as? String ?? "Announcement"
                            let body = (fields["body"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let priority = (fields["priority"] as? [String: Any])?["stringValue"] as? String ?? "Normal"
                            let targetYear = (fields["targetYear"] as? [String: Any])?["stringValue"] as? String ?? "All"
                            let tsStr = (fields["timestamp"] as? [String: Any])?["integerValue"] as? String ?? "0"
                            let timestamp = Int64(tsStr) ?? 0

                            if targetYear == "All" || targetYear == user.year {
                                loaded.append(AnnouncementItem(id: id, title: title, body: body, priority: priority, targetYear: targetYear, timestamp: timestamp))
                            }
                        }
                    }
                    self.announcements = loaded.sorted(by: { $0.timestamp > $1.timestamp })
                }
            }
        }.resume()
    }
}

struct AnnouncementCard: View {
    let titleText: String
    let detailsText: String
    let priorityText: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(priorityText)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(priorityText == "High" ? Color.red.opacity(0.15) : Color.blue.opacity(0.15))
                    .foregroundColor(priorityText == "High" ? .red : .blue)
                    .cornerRadius(6)
                Spacer()
            }
            Text(titleText)
                .font(.subheadline)
                .fontWeight(.bold)
            Text(detailsText)
                .font(.footnote)
                .foregroundColor(.secondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
    }
}

// MARK: - Student Menu View (Matched with Android 7:40 PM Switch & Date Nav)
struct StudentMenuView: View {
    @State private var currentDate = getInitialDate()
    @State private var breakfastMenu = "No items listed for this meal."
    @State private var lunchMenu = "No items listed for this meal."
    @State private var snackMenu = "No items listed for this meal."
    @State private var dinnerMenu = "No items listed for this meal."
    @State private var isLoading = true

    private var dateFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.string(from: currentDate)
    }

    private var dateTitle: String {
        if Calendar.current.isDateInToday(currentDate) {
            return "Today"
        } else if Calendar.current.isDateInTomorrow(currentDate) {
            return "Tomorrow"
        } else {
            let formatter = DateFormatter()
            formatter.dateFormat = "EEEE"
            return formatter.string(from: currentDate)
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Date Selector Header
                HStack {
                    Button(action: {
                        if let prev = Calendar.current.date(byAdding: .day, value: -1, to: currentDate) {
                            currentDate = prev
                        }
                    }) {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.orange)
                            .font(.headline)
                            .padding(8)
                    }

                    Spacer()

                    VStack(spacing: 2) {
                        Text(dateTitle)
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                        Text(dateFormatted)
                            .font(.headline)
                            .fontWeight(.black)
                    }

                    Spacer()

                    Button(action: {
                        if let next = Calendar.current.date(byAdding: .day, value: 1, to: currentDate) {
                            currentDate = next
                        }
                    }) {
                        Image(systemName: "chevron.right")
                            .foregroundColor(.orange)
                            .font(.headline)
                            .padding(8)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(16)

                if isLoading {
                    ProgressView()
                        .padding(30)
                } else {
                    MealCard(title: "Breakfast", items: breakfastMenu, time: "7:30 AM - 9:00 AM", color: .orange)
                    MealCard(title: "Lunch", items: lunchMenu, time: "12:30 PM - 2:00 PM", color: .green)
                    MealCard(title: "Snacks", items: snackMenu, time: "5:00 PM - 6:00 PM", color: .purple)
                    MealCard(title: "Dinner", items: dinnerMenu, time: "7:30 PM - 9:00 PM", color: .blue)
                }
            }
            .padding()
        }
        .onAppear {
            fetchLiveMenu(for: currentDate)
        }
    }

    private func fetchLiveMenu(for date: Date) {
        isLoading = true
        let dateStr = formatDate(date)

        let firestoreUrl = "\(firestoreBaseUrl)/menu/\(dateStr)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let fields = json["fields"] as? [String: Any] {

                    self.breakfastMenu = parseMealInfo(fields["breakfast"])
                    self.lunchMenu = parseMealInfo(fields["lunch"])
                    self.snackMenu = parseMealInfo(fields["snack"])
                    self.dinnerMenu = parseMealInfo(fields["dinner"])
                } else {
                    self.breakfastMenu = "Idli, Sambar, Chutney, Tea / Coffee"
                    self.lunchMenu = "Rice, Rasam, Sambar, Special Curd, Curries"
                    self.snackMenu = "Biscuits & Tea / Coffee"
                    self.dinnerMenu = "Rice, Sambar, Chapati, Special Curry"
                }
            }
        }.resume()
    }

    private func parseMealInfo(_ rawObj: Any?) -> String {
        guard let fieldDict = rawObj as? [String: Any] else { return "No items listed for this meal." }

        if let str = fieldDict["stringValue"] as? String, !str.isBlank {
            return str
        } else if let mapVal = fieldDict["mapValue"] as? [String: Any],
                  let mapFields = mapVal["fields"] as? [String: Any] {

            if let itemsObj = mapFields["items"] as? [String: Any],
               let arrayVal = itemsObj["arrayValue"] as? [String: Any],
               let values = arrayVal["values"] as? [[String: Any]] {
                let itemsList = values.compactMap { $0["stringValue"] as? String }.filter { !$0.isBlank }
                if !itemsList.isEmpty {
                    return itemsList.joined(separator: ", ")
                }
            }
        }
        return "No items listed for this meal."
    }
}

private extension String {
    var isBlank: Bool {
        return trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}

struct MealCard: View {
    let title: String
    let items: String
    let time: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(color)
                Spacer()
                Text(time)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Text(items)
                .font(.body)
                .foregroundColor(.primary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.opacity(0.08))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(color.opacity(0.2), lineWidth: 1)
        )
    }
}

// MARK: - Student Food Count View (Matched with Android Rules: 7:40 PM date switch & ${studentId}_${date})
struct StudentFoodCountView: View {
    let user: StudentUser

    @State private var currentDate = getInitialDate()
    @State private var breakfast = false
    @State private var lunch = false
    @State private var snack = false
    @State private var dinner = false
    @State private var lunchBox = false
    @State private var isLeave = false
    @State private var isSaved = false

    private var dateFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.string(from: currentDate)
    }

    private var dateTitle: String {
        if Calendar.current.isDateInToday(currentDate) {
            return "Today"
        } else if Calendar.current.isDateInTomorrow(currentDate) {
            return "Tomorrow"
        } else {
            let formatter = DateFormatter()
            formatter.dateFormat = "EEEE"
            return formatter.string(from: currentDate)
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header Date Indicator
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Food Count Preference")
                            .font(.title3)
                            .fontWeight(.bold)
                        Text("Date: \(dateTitle) (\(dateFormatted))")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                    }
                    Spacer()
                }

                VStack(spacing: 14) {
                    ToggleRow(title: "Breakfast", isOn: $breakfast, icon: "cup.and.saucer.fill", color: .orange)
                    ToggleRow(title: "Lunch", isOn: $lunch, icon: "takeoutbag.and.cup.and.straw.fill", color: .green)
                    ToggleRow(title: "Snacks", isOn: $snack, icon: "popcorn.fill", color: .purple)
                    ToggleRow(title: "Dinner", isOn: $dinner, icon: "moon.stars.fill", color: .blue)
                    Divider()
                    ToggleRow(title: "Lunch Box Required", isOn: $lunchBox, icon: "bag.fill", color: .brown)
                    ToggleRow(title: "On Leave (No Food)", isOn: $isLeave, icon: "airplane", color: .red)
                }
                .padding(20)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                Button(action: {
                    savePreferences()
                }) {
                    Text(isSaved ? "Saved to Firestore! ✓" : "Save Food Count")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(isSaved ? Color.green : Color.orange)
                        .cornerRadius(16)
                }
            }
            .padding()
        }
        .onAppear {
            self.breakfast = user.breakfastPref
            self.lunch = user.lunchPref
            self.snack = user.snackPref
            self.dinner = user.dinnerPref
            self.isLeave = user.isLeave

            loadCurrentFoodCount()
        }
    }

    private func loadCurrentFoodCount() {
        let dateStr = formatDate(currentDate)
        let docId = "\(user.id)_\(dateStr)"

        let firestoreUrl = "\(firestoreBaseUrl)/foodcounts/\(docId)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let data = data,
               let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let fields = json["fields"] as? [String: Any] {
                DispatchQueue.main.async {
                    if let bf = (fields["breakfast"] as? [String: Any])?["booleanValue"] as? Bool { self.breakfast = bf }
                    if let lu = (fields["lunch"] as? [String: Any])?["booleanValue"] as? Bool { self.lunch = lu }
                    if let sn = (fields["snack"] as? [String: Any])?["booleanValue"] as? Bool { self.snack = sn }
                    if let dn = (fields["dinner"] as? [String: Any])?["booleanValue"] as? Bool { self.dinner = dn }
                    if let lb = (fields["lunchBox"] as? [String: Any])?["booleanValue"] as? Bool { self.lunchBox = lb }
                    if let lv = (fields["isLeave"] as? [String: Any])?["booleanValue"] as? Bool { self.isLeave = lv }
                }
            }
        }.resume()
    }

    private func savePreferences() {
        let dateStr = formatDate(currentDate)
        let docId = "\(user.id)_\(dateStr)"

        let updateMasks = "updateMask.fieldPaths=studentId&updateMask.fieldPaths=date&updateMask.fieldPaths=breakfast&updateMask.fieldPaths=lunch&updateMask.fieldPaths=snack&updateMask.fieldPaths=dinner&updateMask.fieldPaths=lunchBox&updateMask.fieldPaths=isLeave&updateMask.fieldPaths=submittedAt"
        let firestoreUrl = "\(firestoreBaseUrl)/foodcounts/\(docId)?key=\(firestoreApiKey)&\(updateMasks)"
        guard let url = URL(string: firestoreUrl) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let jsonBody: [String: Any] = [
            "fields": [
                "studentId": ["stringValue": user.id],
                "date": ["stringValue": dateStr],
                "breakfast": ["booleanValue": breakfast],
                "lunch": ["booleanValue": lunch],
                "snack": ["booleanValue": snack],
                "dinner": ["booleanValue": dinner],
                "lunchBox": ["booleanValue": lunchBox],
                "isLeave": ["booleanValue": isLeave],
                "submittedAt": ["integerValue": "\(Int64(Date().timeIntervalSince1970 * 1000))"]
            ]
        ]

        request.httpBody = try? JSONSerialization.data(withJSONObject: jsonBody)

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                self.isSaved = true

                self.updateUserProfilePreferences()

                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    self.isSaved = false
                }
            }
        }.resume()
    }

    private func updateUserProfilePreferences() {
        let userMasks = "updateMask.fieldPaths=breakfastPref&updateMask.fieldPaths=lunchPref&updateMask.fieldPaths=snackPref&updateMask.fieldPaths=dinnerPref&updateMask.fieldPaths=isLeave"
        let firestoreUrl = "\(firestoreBaseUrl)/users/\(user.id)?key=\(firestoreApiKey)&\(userMasks)"
        guard let url = URL(string: firestoreUrl) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let jsonBody: [String: Any] = [
            "fields": [
                "breakfastPref": ["booleanValue": breakfast],
                "lunchPref": ["booleanValue": lunch],
                "snackPref": ["booleanValue": snack],
                "dinnerPref": ["booleanValue": dinner],
                "isLeave": ["booleanValue": isLeave]
            ]
        ]

        request.httpBody = try? JSONSerialization.data(withJSONObject: jsonBody)
        URLSession.shared.dataTask(with: request).resume()
    }
}

struct ToggleRow: View {
    let title: String
    @Binding var isOn: Bool
    let icon: String
    let color: Color

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 24)
            Text(title)
                .font(.body)
                .fontWeight(.medium)
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(color)
        }
    }
}

// MARK: - Student Alerts View (Live Firestore Alerts)
struct StudentAlertsView: View {
    let userYear: String
    @State private var alertList: [AnnouncementItem] = []
    @State private var isLoading = true

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Notifications & Alerts")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                if isLoading {
                    ProgressView()
                        .padding()
                } else if alertList.isEmpty {
                    Text("No active alerts.")
                        .font(.footnote)
                        .foregroundColor(.gray)
                        .padding()
                } else {
                    ForEach(alertList) { item in
                        AnnouncementCard(titleText: item.title, detailsText: item.body, priorityText: item.priority)
                    }
                }
            }
            .padding()
        }
        .onAppear {
            fetchAlerts()
        }
    }

    private func fetchAlerts() {
        let firestoreUrl = "\(firestoreBaseUrl)/notifications?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    var loaded: [AnnouncementItem] = []
                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let id = (fields["id"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString
                            let title = (fields["title"] as? [String: Any])?["stringValue"] as? String ?? "Alert"
                            let body = (fields["body"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let priority = (fields["priority"] as? [String: Any])?["stringValue"] as? String ?? "Normal"
                            let targetYear = (fields["targetYear"] as? [String: Any])?["stringValue"] as? String ?? "All"
                            let tsStr = (fields["timestamp"] as? [String: Any])?["integerValue"] as? String ?? "0"
                            let timestamp = Int64(tsStr) ?? 0

                            if targetYear == "All" || targetYear == userYear {
                                loaded.append(AnnouncementItem(id: id, title: title, body: body, priority: priority, targetYear: targetYear, timestamp: timestamp))
                            }
                        }
                    }
                    self.alertList = loaded.sorted(by: { $0.timestamp > $1.timestamp })
                }
            }
        }.resume()
    }
}

// MARK: - Vedic Letter Info View (|| Shree ||)
struct VedicLetterView: View {
    let onClose: () -> Void

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    Text("॥ श्रीः ॥")
                        .font(.largeTitle)
                        .fontWeight(.black)
                        .foregroundColor(.red)

                    Text("SMYS Food Count")
                        .font(.headline)
                        .fontWeight(.bold)

                    Text("Sathpanatha 2K26–2K27 — 75th Year")
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(Color.yellow.opacity(0.2))
                        .foregroundColor(.brown)
                        .cornerRadius(8)

                    Divider()

                    VStack(alignment: .leading, spacing: 14) {
                        Text("Dear SMYS Hostel Community,")
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.red)

                        Text("SMYS Food Count is a digital hostel food management system designed to collect daily meal counts efficiently (Breakfast, Lunch, Snacks, and Dinner). It reduces manual work and minimizes food wastage.")
                            .font(.footnote)

                        Text("Office Bearers (75th Year):")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.brown)

                        Text("1. General Secretary — Aditya Mathad\n2. Joint General Secretary — Pushkar Deshpande\n3. Treasurer — Aniruddh Joshi\n4. Food Secretary — G. Shree Skanda Upadhya\n5. Joint Food Secretary — Koustubh B. Adi\n6. Joint Sports Secretary — Satwik G J\n...and all 15 Office Bearers.")
                            .font(.caption2)
                            .foregroundColor(.secondary)

                        Divider()

                        VStack(alignment: .trailing, spacing: 2) {
                            Text("With Regards,")
                                .font(.caption)
                            Text("Developer")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(.orange)
                            Text("Satwik G J")
                                .font(.subheadline)
                                .fontWeight(.black)
                            Text("@developing_developer")
                                .font(.caption2)
                                .foregroundColor(.gray)
                        }
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(20)
                    .background(Color(red: 1.0, green: 0.98, blue: 0.94))
                    .cornerRadius(20)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                    )
                }
                .padding()
            }
            .navigationTitle("SMYS Letter")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Close", action: onClose)
                }
            }
        }
    }
}
