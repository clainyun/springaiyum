export type MessageResponse = {
  message: string
}

export type UserSummary = {
  id: string
  email: string
  nickname: string
  active: boolean
}

export type UserProfile = UserSummary & {
  gender: string
  birthYear: number
  height: number
  weight: number
  goal: string
  healthNote: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  email: string
  password: string
  nickname: string
  gender: string
  birthYear: number | null
  height: number | null
  weight: number | null
  goal: string
  healthNote: string
}

export type AuthResponse = {
  message: string
  user: UserSummary
}

export type UpdateUserProfileRequest = {
  email: string
  nickname: string
  password: string
  gender: string
  birthYear: number | null
  height: number | null
  weight: number | null
  goal: string
  healthNote: string
}

export type FoodResponse = {
  code: string
  name: string
  category: string
  grams: number
  energy: number
  carbs: number
  protein: number
  fat: number
}

export type MealNutrition = {
  calories: number
  carbs: number
  protein: number
  fat: number
  carbsPct: number
  proteinPct: number
  fatPct: number
}

export type MealFood = FoodResponse

export type MealFoodSelection = {
  code: string
  grams: number | null
}

export type MealRequest = {
  mealDate: string
  mealType: string
  memo: string
  foods: MealFoodSelection[]
}

export type MealSummary = {
  id: string
  mealDate: string
  mealType: string
  memo: string
  nutrition: MealNutrition
}

export type DailyGoal = {
  calories: number
  carbs: number
  protein: number
  fat: number
}

export type MealAnalysis = {
  nutrition: MealNutrition
  headline: string
  nextAction: string
  grade: string
  score: number
  insights: string[]
}

export type MealDetail = MealSummary & {
  userId: string
  foods: MealFood[]
  analysis: MealAnalysis | null
  dailyGoal: DailyGoal | null
  createdAt: string
  updatedAt: string
}

export type MealSearchParams = {
  startDate?: string
  endDate?: string
  mealType?: string
  sortKey?: string
}
